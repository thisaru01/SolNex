using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs;

public class SlotDto
{
    public string? Id { get; set; }
    public string SlotId { get; set; } = null!;
    public string StationId { get; set; } = null!;
    public DateTime SlotDate { get; set; }
    public string StartTime { get; set; } = null!;
    public string EndTime { get; set; } = null!;
    public string SlotStatus { get; set; } = null!;
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }
}
