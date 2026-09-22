using SolNex.Api.DTOs;
using SolNex.Api.Models;
using SolNex.Api.Repositories;

namespace SolNex.Api.Services;

public class EnergyBookingSlotService : IEnergyBookingSlotService
{
    private readonly IEnergyBookingSlotRepository _slotRepository;

    public EnergyBookingSlotService(IEnergyBookingSlotRepository slotRepository)
    {
        _slotRepository = slotRepository;
    }

    public async Task<IEnumerable<SlotDto>> GetAllSlotsAsync()
    {
        var slots = await _slotRepository.GetAllSlotsAsync();
        return slots.Select(MapToDto);
    }

    public async Task<IEnumerable<SlotDto>> GetSlotsByStationIdAsync(string stationId)
    {
        var slots = await _slotRepository.GetSlotsByStationIdAsync(stationId);
        return slots.Select(MapToDto);
    }

    public async Task<IEnumerable<SlotDto>> GetAvailableSlotsAsync()
    {
        var slots = await _slotRepository.GetAvailableSlotsAsync();
        return slots.Select(MapToDto);
    }

    public async Task<SlotDto?> GetSlotByIdAsync(string id)
    {
        var slot = await _slotRepository.GetSlotByIdAsync(id)
                ?? await _slotRepository.GetSlotBySlotIdAsync(id);

        return slot != null ? MapToDto(slot) : null;
    }

    public async Task<SlotDto> CreateSlotAsync(CreateSlotDto createDto)
    {
        var slotId = $"{createDto.StationId}_{createDto.SlotDate:yyyyMMdd}_{createDto.StartTime.Replace(":", "")}";

        var slot = new EnergyBookingSlot
        {
            SlotId = slotId,
            StationId = createDto.StationId,
            SlotDate = createDto.SlotDate,
            StartTime = createDto.StartTime,
            EndTime = createDto.EndTime,
            SlotStatus = SlotStatus.Available,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow
        };

        await _slotRepository.CreateSlotAsync(slot);
        return MapToDto(slot);
    }

    public async Task DeleteSlotAsync(string id)
    {
        var slot = await _slotRepository.GetSlotByIdAsync(id)
                ?? await _slotRepository.GetSlotBySlotIdAsync(id);

        if (slot != null && slot.Id != null)
        {
            await _slotRepository.DeleteSlotAsync(slot.Id);
        }
    }

    private SlotDto MapToDto(EnergyBookingSlot slot)
    {
        return new SlotDto
        {
            Id = slot.Id,
            SlotId = slot.SlotId,
            StationId = slot.StationId,
            SlotDate = slot.SlotDate,
            StartTime = slot.StartTime,
            EndTime = slot.EndTime,
            SlotStatus = slot.SlotStatus.ToString(),
            CreatedAt = slot.CreatedAt,
            UpdatedAt = slot.UpdatedAt
        };
    }
}
