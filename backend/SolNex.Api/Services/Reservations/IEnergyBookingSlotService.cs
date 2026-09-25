using SolNex.Api.DTOs.Reservations;
using SolNex.Api.Services.Reservations;
using SolNex.Api.Repositories.Reservations;
using SolNex.Api.DTOs;

namespace SolNex.Api.Services.Reservations;

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

