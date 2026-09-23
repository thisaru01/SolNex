using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs;

public sealed class RegisterUserRequest
{
    [Required]
    [StringLength(20, MinimumLength = 5)]
    public string Nic { get; set; } = string.Empty;

    [Required]
    [StringLength(120, MinimumLength = 2)]
    public string FullName { get; set; } = string.Empty;

    [Required]
    [EmailAddress]
    public string Email { get; set; } = string.Empty;

    [Required]
    [Phone]
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
