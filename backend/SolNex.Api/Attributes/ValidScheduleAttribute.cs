using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Text.RegularExpressions;

namespace SolNex.Api.Attributes;

public class ValidScheduleAttribute : ValidationAttribute
{
    protected override ValidationResult? IsValid(object? value, ValidationContext validationContext)
    {
        if (value == null) return ValidationResult.Success;

        if (value is Dictionary<string, string> schedule)
        {
            var regex = new Regex(@"^(?<startH>0[0-9]|1[0-9]|2[0-3]|[0-9])[:.](?<startM>[0-5][0-9])\s*-\s*(?<endH>0[0-9]|1[0-9]|2[0-3]|[0-9])[:.](?<endM>[0-5][0-9])$", RegexOptions.Compiled);

            foreach (var kvp in schedule)
            {
                var timeStr = kvp.Value?.Trim();
                
                if (string.IsNullOrEmpty(timeStr)) continue;
                if (timeStr.Equals("Closed", StringComparison.OrdinalIgnoreCase)) continue;

                var match = regex.Match(timeStr);
                if (!match.Success)
                {
                    return new ValidationResult($"Invalid time format for '{kvp.Key}'. Expected format is 'HH:mm - HH:mm' (e.g. 09:00 - 17:00) or 'Closed'.");
                }

                int startH = int.Parse(match.Groups["startH"].Value);
                int startM = int.Parse(match.Groups["startM"].Value);
                int endH = int.Parse(match.Groups["endH"].Value);
                int endM = int.Parse(match.Groups["endM"].Value);

                TimeSpan startTime = new TimeSpan(startH, startM, 0);
                TimeSpan endTime = new TimeSpan(endH, endM, 0);

                if (startTime >= endTime)
                {
                    return new ValidationResult($"Start time cannot be greater than or equal to end time for '{kvp.Key}'.");
                }
            }
        }

        return ValidationResult.Success;
    }
}
