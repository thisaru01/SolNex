/*
 * ------------------------------------------------------------------
 * File Name: IEnergyBookingSlotRepository.cs
 * Author: Kavindi P.D.I
 * Student ID: IT23234666
 * Date: 2026-09-25
 * Description: Repository interface for data access operations related to EnergyBookingSlot.
 * ------------------------------------------------------------------
 */

using SolNex.Api.Models;

namespace SolNex.Api.Repositories.Reservations;

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
