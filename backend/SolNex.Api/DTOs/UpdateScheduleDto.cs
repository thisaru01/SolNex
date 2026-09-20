using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs;

public class UpdateScheduleDto
{
    [Required]
    public Dictionary<string, string> Schedule { get; set; } = new();
}
