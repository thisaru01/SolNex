/*
 * ------------------------------------------------------------------
 * File Name: UpdateBatterySlotsDto.cs
 * Author: Rajapaksha T.M
 * Student ID: IT23235892
 * Date: 2026-09-24
 * Description: Data transfer object for updating battery slots.
 * ------------------------------------------------------------------
 */

using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs.Stations;

public class UpdateBatterySlotsDto
{
    [Required]
    [Range(0, int.MaxValue)]
    public int AvailableBatterySlots { get; set; }
}
