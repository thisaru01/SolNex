using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs;

public sealed class LoginRequest
{
    [Required]
    public string Identifier { get; set; } = string.Empty;

    [Required]
    public string Password { get; set; } = string.Empty;
}

public sealed record LoginResponse(
    string Token,
    DateTime ExpiresAt,
    string Nic,
    string FullName,
    string Email,
    string Role);