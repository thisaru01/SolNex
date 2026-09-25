using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs.Reservations;

public class ReservationDto
{
    public string? Id { get; set; }
    public string ReservationId { get; set; } = null!;
    public string Nic { get; set; } = null!;
    public string StationId { get; set; } = null!;
    public string SlotId { get; set; } = null!;
    public DateTime ReservationDate { get; set; }
    public double EnergyAmountKwh { get; set; }
    public string Status { get; set; } = null!;
    public string? ApprovedBy { get; set; }
    public DateTime? ApprovedAt { get; set; }
    public string? RejectedReason { get; set; }
    public string? StartTime { get; set; }
    public string? EndTime { get; set; }
    public string? DayOfWeek { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }
}
