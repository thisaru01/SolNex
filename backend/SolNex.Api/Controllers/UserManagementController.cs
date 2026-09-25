using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SolNex.Api.DTOs;
using SolNex.Api.Models;
using SolNex.Api.Services;

namespace SolNex.Api.Controllers;

[ApiController]
[Route("api/users")]
[Authorize]
public sealed class UserManagementController : ControllerBase
{
    private readonly IUserService _userService;

    public UserManagementController(IUserService userService)
    {
        _userService = userService;
    }

    [HttpGet]
    [Authorize(Roles = "Backoffice")]
    public Task<IReadOnlyList<UserListItem>> GetUsers([FromQuery] string? search, CancellationToken cancellationToken) =>
        _userService.GetUsersAsync(search, cancellationToken);

    [HttpGet("pending")]
    [Authorize(Roles = "Backoffice")]
    public Task<IReadOnlyList<UserListItem>> GetPendingUsers(CancellationToken cancellationToken) =>
        _userService.GetPendingUsersAsync(cancellationToken);

    [HttpGet("{nic}")]
    public async Task<IActionResult> GetUser(string nic, CancellationToken cancellationToken)
    {
        if (!CanAccessUser(nic))
        {
            return Forbid();
        }

        var user = await _userService.GetUserAsync(nic, cancellationToken);
        return user is null ? NotFound() : Ok(user);
    }

    [HttpPut("{nic}")]
    public async Task<IActionResult> UpdateUser(string nic, UpdateUserRequest request, CancellationToken cancellationToken)
    {
        if (!CanAccessUser(nic))
        {
            return Forbid();
        }

        var user = await _userService.UpdateUserAsync(nic, request, cancellationToken);
        return user is null ? NotFound() : Ok(user);
    }

    [HttpPut("{nic}/role")]
    [Authorize(Roles = "Backoffice")]
    public async Task<IActionResult> UpdateRole(string nic, UpdateUserRoleRequest request, CancellationToken cancellationToken)
    {
        var user = await _userService.UpdateRoleAsync(nic, request, cancellationToken);
        return user is null ? BadRequest(new { message = "User or role is invalid." }) : Ok(user);
    }

    [HttpPut("{nic}/activate")]
    [Authorize(Roles = "Backoffice")]
    public Task<IActionResult> Activate(string nic, CancellationToken cancellationToken) => SetStatus(nic, AccountStatus.Active, cancellationToken);

    [HttpPut("{nic}/deactivate")]
    public async Task<IActionResult> Deactivate(string nic, CancellationToken cancellationToken)
    {
        if (!User.IsInRole("Backoffice") && !string.Equals(User.FindFirstValue(ClaimTypes.NameIdentifier), nic, StringComparison.OrdinalIgnoreCase))
        {
            return Forbid();
        }

        return await SetStatus(nic, AccountStatus.Inactive, cancellationToken);
    }

    private async Task<IActionResult> SetStatus(string nic, AccountStatus status, CancellationToken cancellationToken)
    {
        var user = await _userService.SetStatusAsync(nic, status, cancellationToken);
        return user is null ? NotFound() : Ok(user);
    }

    private bool CanAccessUser(string nic)
    {
        if (User.IsInRole("Backoffice"))
        {
            return true;
        }

        var currentNic = User.FindFirstValue(ClaimTypes.NameIdentifier);
        return !string.IsNullOrWhiteSpace(currentNic)
            && string.Equals(currentNic, nic, StringComparison.OrdinalIgnoreCase);
    }
}