/*
 * File Name: RegisterUserRequest.cs
 * Author: Vidanapathirana L S
 * Student ID: IT23442498
 * Description: Data transfer object for user registration requests.
 */
// User registration DTOs
using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs;

public sealed class RegisterUserRequest
{
    [Required]
    [RegularExpression(@"^(\d{9}[VX]|\d{12})$", ErrorMessage = "NIC must be 9 digits ending with V/X or 12 digits.")]
    public string Nic { get; set; } = string.Empty;

    [Required]
    [StringLength(120, MinimumLength = 2)]
    public string FullName { get; set; } = string.Empty;

    [Required]
    [EmailAddress]
    public string Email { get; set; } = string.Empty;

    [Required]
    [RegularExpression(@"^(94\d{9}|0\d{9})$", ErrorMessage = "Phone must be 94 followed by 9 digits or 0 followed by 9 digits.")]
    public string Phone { get; set; } = string.Empty;

    [Required]
    [StringLength(100, MinimumLength = 8)]
    public string Password { get; set; } = string.Empty;
}

public sealed record RegisteredUserResponse(
    string Nic,
    string FullName,
    string Email,
    string Phone,
    string Role,
    string AccountStatus,
    DateTime CreatedAt);

public sealed class RegisterWebUserRequest
{
    [Required]
    [RegularExpression(@"^(\d{9}[VX]|\d{12})$", ErrorMessage = "NIC must be 9 digits ending with V/X or 12 digits.")]
    public string Nic { get; set; } = string.Empty;

    [Required]
    [StringLength(120, MinimumLength = 2)]
    public string FullName { get; set; } = string.Empty;

    [Required]
    [EmailAddress]
    public string Email { get; set; } = string.Empty;

    [Required]
    [RegularExpression(@"^(94\d{9}|0\d{9})$", ErrorMessage = "Phone must be 94 followed by 9 digits or 0 followed by 9 digits.")]
    public string Phone { get; set; } = string.Empty;

    [Required]
    [StringLength(100, MinimumLength = 8)]
    public string Password { get; set; } = string.Empty;

    [Required]
    public string Role { get; set; } = string.Empty;
}
