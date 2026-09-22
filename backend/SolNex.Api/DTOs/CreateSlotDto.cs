using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs;

public class CreateSlotDto : IValidatableObject
{
    [Required]
    public string StationId { get; set; } = null!;

    [Required]
    public DateTime SlotDate { get; set; }

    [Required]
    public string StartTime { get; set; } = null!;

    [Required]
    public string EndTime { get; set; } = null!;

    [Required]
    public string DayOfWeek { get; set; } = null!;

    public IEnumerable<ValidationResult> Validate(ValidationContext validationContext)
    {
        if (DateTime.TryParse(StartTime, out var start) && DateTime.TryParse(EndTime, out var end))
        {
            if (end <= start)
            {
                yield return new ValidationResult("EndTime must be after StartTime.", new[] { nameof(EndTime) });
            }
        }
        else
        {
            yield return new ValidationResult("Invalid time format. Please provide valid times.", new[] { nameof(StartTime), nameof(EndTime) });
        }
    }
}
