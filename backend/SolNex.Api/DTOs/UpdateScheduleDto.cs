using System.ComponentModel.DataAnnotations;
using SolNex.Api.Attributes;

namespace SolNex.Api.DTOs;

public class UpdateScheduleDto
{
    [Required]
    [ValidSchedule]
    public Dictionary<string, string> Schedule { get; set; } = new();
}
