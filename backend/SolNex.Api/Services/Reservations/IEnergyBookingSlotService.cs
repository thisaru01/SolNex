/*
 * ------------------------------------------------------------------
 * File Name: IEnergyBookingSlotService.cs
 * Author: Kavindi P.D.I
 * Student ID: IT23234666
 * Date: 2026-09-25
 * Description: Service interface for business logic related to EnergyBookingSlot.
 * ------------------------------------------------------------------
 */

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
    Task<SlotDto> CreateSlotAsync(CreateSlotDto createDto, string? backofficerId = null, string? backofficerName = null);
    Task<SlotDto?> UpdateSlotStatusAsync(string id, string status);
    Task<SlotDto?> UpdateSlotTimeAsync(string id, string startTime, string endTime);
    Task DeleteSlotAsync(string id);
}

