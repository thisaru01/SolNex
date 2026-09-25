using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs.Reservations;

public class UpdateSlotStatusDto
{
    [Required]
    public string Status { get; set; } = null!;
}
