using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs;

public class UpdateReservationDto
{
    public string? Status { get; set; }
    public string? ApprovedBy { get; set; }
    public DateTime? ApprovedAt { get; set; }
    public string? RejectedReason { get; set; }
}
