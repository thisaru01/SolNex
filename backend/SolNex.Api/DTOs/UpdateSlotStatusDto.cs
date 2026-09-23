using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs;

public class UpdateSlotStatusDto
{
    [Required]
    public string Status { get; set; } = null!;
}
