/*
 * ------------------------------------------------------------------
 * File Name: UpdateScheduleDto.cs
 * Author: Rajapaksha T.M
 * Student ID: IT23235892
 * Date: 2026-09-24
 * Description: Data transfer object for updating a schedule.
 * ------------------------------------------------------------------
 */

using System.ComponentModel.DataAnnotations;
using SolNex.Api.Attributes;

namespace SolNex.Api.DTOs.Stations;

public class UpdateScheduleDto
{
    [Required]
    [ValidSchedule]
    public Dictionary<string, string> Schedule { get; set; } = new();
}
