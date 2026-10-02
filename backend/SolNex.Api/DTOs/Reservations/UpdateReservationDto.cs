/*
 * ------------------------------------------------------------------
 * File Name: UpdateReservationDto.cs
 * Author: Kavindi P.D.I
 * Student ID: IT23234666
 * Date: 2026-09-25
 * Description: Data Transfer Object for UpdateReservation operations.
 * ------------------------------------------------------------------
 */

using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs.Reservations;

public class UpdateReservationDto
{
    public string? SlotId { get; set; }
    public double? EnergyAmountKwh { get; set; }
    public string? Status { get; set; }
    public string? ApprovedBy { get; set; }
    public DateTime? ApprovedAt { get; set; }
    public string? RejectedReason { get; set; }
    public bool? IsCancellationRejected { get; set; }
}
