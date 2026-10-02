/*
 * ------------------------------------------------------------------
 * File Name: UpdateSlotStatusDto.cs
 * Author: Kavindi P.D.I
 * Student ID: IT23234666
 * Date: 2026-09-25
 * Description: Data Transfer Object for UpdateSlotStatus operations.
 * ------------------------------------------------------------------
 */

using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs.Reservations;

public class UpdateSlotStatusDto
{
    [Required]
    public string Status { get; set; } = null!;
}
