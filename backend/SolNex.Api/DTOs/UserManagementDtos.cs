using System.ComponentModel.DataAnnotations;

namespace SolNex.Api.DTOs;

public sealed record UserListItem(
    string Nic,
    string FullName,
    string Email,
    string Phone,
    string Role,
    string AccountStatus,
    DateTime CreatedAt,
    DateTime UpdatedAt);

public sealed class UpdateUserRequest
{
    [Required]
    [StringLength(120, MinimumLength = 2)]
    public string FullName { get; set; } = string.Empty;

    [Required]
    [EmailAddress]
    public string Email { get; set; } = string.Empty;

    [Required]
    [Phone]
    public string Phone { get; set; } = string.Empty;
}

public sealed class UpdateUserRoleRequest
{
    [Required]
    public string Role { get; set; } = string.Empty;
}