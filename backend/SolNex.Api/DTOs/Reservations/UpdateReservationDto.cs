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
}
