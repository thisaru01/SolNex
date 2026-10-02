/*
 * ------------------------------------------------------------------
 * File Name: SlotDto.cs
 * Author: Kavindi P.D.I
 * Student ID: IT23234666
 * Date: 2026-09-25
 * Description: Data Transfer Object for Slot operations.
 * ------------------------------------------------------------------
 */

using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs.Reservations;

public class SlotDto
{
    public string? Id { get; set; }
    public string SlotId { get; set; } = null!;
    public string StationId { get; set; } = null!;
    public string StartTime { get; set; } = null!;
    public string EndTime { get; set; } = null!;
    public string? DayOfWeek { get; set; }
    public string? ScheduleTime { get; set; }
    public string SlotStatus { get; set; } = null!;
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }
    public string? CreatedBy { get; set; }
    public string? CreatorName { get; set; }
}
