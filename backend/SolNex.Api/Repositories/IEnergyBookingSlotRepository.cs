using SolNex.Api.Models;

namespace SolNex.Api.Repositories;

public interface IEnergyBookingSlotRepository
{
    Task<IEnumerable<EnergyBookingSlot>> GetAllSlotsAsync();
    Task<IEnumerable<EnergyBookingSlot>> GetSlotsByStationIdAsync(string stationId);
    Task<IEnumerable<EnergyBookingSlot>> GetAvailableSlotsAsync();
    Task<EnergyBookingSlot?> GetSlotByIdAsync(string id);
    Task<EnergyBookingSlot?> GetSlotBySlotIdAsync(string slotId);
    Task CreateSlotAsync(EnergyBookingSlot slot);
    Task UpdateSlotAsync(string id, EnergyBookingSlot slot);
    Task DeleteSlotAsync(string id);
}
