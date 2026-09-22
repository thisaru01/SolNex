using MongoDB.Driver;
using SolNex.Api.DTOs;
using SolNex.Api.Models;
using SolNex.Api.Repositories;

namespace SolNex.Api.Services;

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

        // Prevent duplicate slot creation for the same station, day of week, and time
        var existingSlot = await _slotRepository.GetSlotBySlotIdAsync(slotId);
        if (existingSlot != null)
        {
            throw new InvalidOperationException($"A slot already exists for station '{createDto.StationId}' on {createDto.DayOfWeek} at {createDto.StartTime}.");
        }

        var scheduleDict = await _stationService.GetStationScheduleAsync(createDto.StationId);

        // Validate that requested slot falls within the station's operating hours for the given day
        var scheduleTime = ValidateSlotAgainstSchedule(createDto.DayOfWeek, createDto.StartTime, createDto.EndTime, scheduleDict);

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
