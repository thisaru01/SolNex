/*
 * File Name: LoginRequest.cs
 * Author: Vidanapathirana L S
 * Student ID: IT23442498
 * Description: Data transfer object for login requests.
 */
using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs;

public sealed class LoginRequest
{
    [Required]
    public string Identifier { get; set; } = string.Empty;

    [Required]
    public string Password { get; set; } = string.Empty;
}

public sealed class ResetPasswordRequest
{
    [Required]
    public string Identifier { get; set; } = string.Empty;

    [Required, EmailAddress]
    public string Email { get; set; } = string.Empty;

    [Required, MinLength(8)]
    public string NewPassword { get; set; } = string.Empty;
}

public sealed record LoginResponse(
    string Token,
    DateTime ExpiresAt,
    string Nic,
    string FullName,
    string Email,
    string Role);