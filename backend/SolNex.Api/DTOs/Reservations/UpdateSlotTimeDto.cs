/*
 * ------------------------------------------------------------------
 * File Name: UpdateSlotTimeDto.cs
 * Author: Kavindi P.D.I
 * Student ID: IT23234666
 * Date: 2026-09-28
 * Description: Data Transfer Object for UpdateSlotTime operations.
 * ------------------------------------------------------------------
 */

using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs.Reservations;

public class UpdateSlotTimeDto : IValidatableObject
{
    [Required]
    public string StartTime { get; set; } = null!;

    [Required]
    public string EndTime { get; set; } = null!;

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
