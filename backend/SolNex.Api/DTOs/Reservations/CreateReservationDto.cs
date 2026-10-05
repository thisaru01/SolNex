/*
 * ------------------------------------------------------------------
 * File Name: CreateReservationDto.cs
 * Author: Kavindi P.D.I
 * Student ID: IT23234666
 * Date: 2026-09-25
 * Description: Data Transfer Object for CreateReservation operations.
 * ------------------------------------------------------------------
 */

using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs.Reservations;

public class CreateReservationDto
{
    [Required]
    public string Nic { get; set; } = null!;

    [Required]
    public string StationId { get; set; } = null!;

    [Required]
    public string SlotId { get; set; } = null!;

    [Required]
    public DateTime ReservationDate { get; set; }

    [Required]
    [Range(0.1, double.MaxValue, ErrorMessage = "Energy amount must be greater than 0")]
    public double EnergyAmountKwh { get; set; }
}
