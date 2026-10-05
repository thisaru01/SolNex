/*
 * ------------------------------------------------------------------
 * File Name: UpdateReservationDetailsDto.cs
 * Author: Kavindi P.D.I
 * Student ID: IT23234666
 * Date: 2026-09-25
 * Description: Data Transfer Object for UpdateReservationDetails operations.
 * ------------------------------------------------------------------
 */

namespace SolNex.Api.DTOs.Reservations;

public class UpdateReservationDetailsDto
{
    public string? SlotId { get; set; }
    public double? EnergyAmountKwh { get; set; }
}
