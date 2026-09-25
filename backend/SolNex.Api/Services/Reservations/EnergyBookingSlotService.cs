using SolNex.Api.DTOs.Reservations;
using SolNex.Api.Services.Reservations;
using SolNex.Api.Repositories.Reservations;
using MongoDB.Driver;
using SolNex.Api.DTOs;
using SolNex.Api.Models;
using SolNex.Api.Repositories;
using SolNex.Api.Services.Stations;

namespace SolNex.Api.Services.Reservations;


public class EnergyBookingSlotService : IEnergyBookingSlotService
{
    private readonly IEnergyBookingSlotRepository _slotRepository;
    private readonly IStationService _stationService;

    // Injects required repository and station service abstractions (Dependency Inversion Principle)
    public EnergyBookingSlotService(IEnergyBookingSlotRepository slotRepository, IStationService stationService)
    {
        _slotRepository = slotRepository;
        _stationService = stationService;
    }

    // Fetches all energy booking slots and maps them to DTOs
    public async Task<IEnumerable<SlotDto>> GetAllSlotsAsync()
    {
        var slots = await _slotRepository.GetAllSlotsAsync();
        return slots.Select(MapToDto);
    }

    // Fetches all booking slots belonging to a specified station
    public async Task<IEnumerable<SlotDto>> GetSlotsByStationIdAsync(string stationId)
    {
        var slots = await _slotRepository.GetSlotsByStationIdAsync(stationId);
        return slots.Select(MapToDto);
    }

    // Fetches only available slots for booking across stations
    public async Task<IEnumerable<SlotDto>> GetAvailableSlotsAsync()
    {
        var slots = await _slotRepository.GetAvailableSlotsAsync();
        return slots.Select(MapToDto);
    }

    // Retrieves a specific slot by its database ID or generated slot ID
    public async Task<SlotDto?> GetSlotByIdAsync(string id)
    {
        var slot = await _slotRepository.GetSlotByIdAsync(id)
                ?? await _slotRepository.GetSlotBySlotIdAsync(id);

        return slot != null ? MapToDto(slot) : null;
    }

    // Validates operating schedule hours and creates a new booking slot
    public async Task<SlotDto> CreateSlotAsync(CreateSlotDto createDto)
    {
        var slotId = $"{createDto.StationId}_{createDto.DayOfWeek}_{createDto.StartTime.Replace(":", "")}";

        var scheduleDict = await _stationService.GetStationScheduleAsync(createDto.StationId);

        // Validate that requested slot falls within the station's operating hours for the given day
        var scheduleTime = ValidateSlotAgainstSchedule(createDto.DayOfWeek, createDto.StartTime, createDto.EndTime, scheduleDict);

        // Prevent overlapping slots for the same station on the same day
        var existingSlots = await _slotRepository.GetSlotsByStationIdAsync(createDto.StationId);
        var slotsOnSameDay = existingSlots.Where(s => string.Equals(s.DayOfWeek, createDto.DayOfWeek, StringComparison.OrdinalIgnoreCase));
        ValidateNoOverlap(createDto.StartTime, createDto.EndTime, slotsOnSameDay);

        var slot = new EnergyBookingSlot
        {
            SlotId = slotId,
            StationId = createDto.StationId,
            StartTime = createDto.StartTime,
            EndTime = createDto.EndTime,
            DayOfWeek = createDto.DayOfWeek,
            ScheduleTime = scheduleTime,
            SlotStatus = SlotStatus.Available,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow
        };

        try
        {
            await _slotRepository.CreateSlotAsync(slot);
        }
        catch (MongoWriteException ex) when (ex.WriteError?.Category == ServerErrorCategory.DuplicateKey)
        {
            throw new InvalidOperationException($"A slot already exists for station '{createDto.StationId}' on {createDto.DayOfWeek} at {createDto.StartTime}.");
        }

        return MapToDto(slot);
    }

    // Updates the status of an energy booking slot (e.g., Available -> Reserved)
    public async Task<SlotDto?> UpdateSlotStatusAsync(string id, string status)
    {
        var slot = await _slotRepository.GetSlotByIdAsync(id)
                ?? await _slotRepository.GetSlotBySlotIdAsync(id);

        if (slot == null || slot.Id == null)
        {
            return null;
        }

        if (!Enum.TryParse<SlotStatus>(status, true, out var newStatus))
        {
            throw new ArgumentException($"Invalid status '{status}'. Valid statuses are: {string.Join(", ", Enum.GetNames<SlotStatus>())}");
        }

        slot.SlotStatus = newStatus;
        slot.UpdatedAt = DateTime.UtcNow;

        await _slotRepository.UpdateSlotAsync(slot.Id, slot);
        return MapToDto(slot);
    }

    // Removes an existing booking slot from repository
    public async Task DeleteSlotAsync(string id)
    {
        var slot = await _slotRepository.GetSlotByIdAsync(id)
                ?? await _slotRepository.GetSlotBySlotIdAsync(id);

        if (slot != null && slot.Id != null)
        {
            await _slotRepository.DeleteSlotAsync(slot.Id);
        }
    }

    // Validates that slot times are within station operating schedule (Single Responsibility Principle)
    private static string ValidateSlotAgainstSchedule(string dayOfWeek, string startTime, string endTime, Dictionary<string, string>? scheduleDict)
    {
        string scheduleTime = "Closed";
        if (scheduleDict != null && scheduleDict.TryGetValue(dayOfWeek, out var time))
        {
            scheduleTime = time;
        }

        if (scheduleTime.Equals("Closed", StringComparison.OrdinalIgnoreCase))
        {
            throw new ArgumentException($"Cannot create a slot on {dayOfWeek} as the station is closed.");
        }

        var timeParts = scheduleTime.Split('-');
        if (timeParts.Length != 2 || 
            !DateTime.TryParse(timeParts[0].Trim(), out var scheduleStart) || 
            !DateTime.TryParse(timeParts[1].Trim(), out var scheduleEnd))
        {
            throw new ArgumentException("Station schedule time format is invalid.");
        }

        if (!DateTime.TryParse(startTime, out var slotStart) ||
            !DateTime.TryParse(endTime, out var slotEnd))
        {
            throw new ArgumentException("Invalid slot time format.");
        }

        if (slotStart.TimeOfDay < scheduleStart.TimeOfDay || slotEnd.TimeOfDay > scheduleEnd.TimeOfDay)
        {
            throw new ArgumentException($"Slot times must be within the station's schedule of {scheduleTime}.");
        }

        return scheduleTime;
    }

    // Checks if the requested time range overlaps with any existing slots
    private static void ValidateNoOverlap(string requestedStartTime, string requestedEndTime, IEnumerable<EnergyBookingSlot> existingSlots)
    {
        if (!DateTime.TryParse(requestedStartTime, out var reqStart) ||
            !DateTime.TryParse(requestedEndTime, out var reqEnd))
        {
            throw new ArgumentException("Invalid requested slot time format.");
        }

        foreach (var slot in existingSlots)
        {
            if (DateTime.TryParse(slot.StartTime, out var slotStart) &&
                DateTime.TryParse(slot.EndTime, out var slotEnd))
            {
                // Overlap condition: Request starts before existing ends AND request ends after existing starts
                if (reqStart.TimeOfDay < slotEnd.TimeOfDay && reqEnd.TimeOfDay > slotStart.TimeOfDay)
                {
                    throw new InvalidOperationException($"The requested time slot ({requestedStartTime} - {requestedEndTime}) overlaps with an existing slot ({slot.StartTime} - {slot.EndTime}).");
                }
            }
        }
    }

    // Maps internal entity to API Data Transfer Object
    private static SlotDto MapToDto(EnergyBookingSlot slot)
    {
        return new SlotDto
        {
            Id = slot.Id,
            SlotId = slot.SlotId,
            StationId = slot.StationId,
            StartTime = slot.StartTime,
            EndTime = slot.EndTime,
            DayOfWeek = slot.DayOfWeek,
            ScheduleTime = slot.ScheduleTime,
            SlotStatus = slot.SlotStatus.ToString(),
            CreatedAt = slot.CreatedAt,
            UpdatedAt = slot.UpdatedAt
        };
    }
}

