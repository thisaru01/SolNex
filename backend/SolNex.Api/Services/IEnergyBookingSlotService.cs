using SolNex.Api.DTOs;

namespace SolNex.Api.Services;

public interface IEnergyBookingSlotService
{
    Task<IEnumerable<SlotDto>> GetAllSlotsAsync();
    Task<IEnumerable<SlotDto>> GetSlotsByStationIdAsync(string stationId);
    Task<IEnumerable<SlotDto>> GetAvailableSlotsAsync();
    Task<SlotDto?> GetSlotByIdAsync(string id);
    Task<SlotDto> CreateSlotAsync(CreateSlotDto createDto);
    Task<SlotDto?> UpdateSlotStatusAsync(string id, string status);
    Task DeleteSlotAsync(string id);
}
