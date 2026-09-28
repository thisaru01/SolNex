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

    [HttpPost("register")]
    [Authorize(Roles = "Backoffice")]
    public async Task<IActionResult> RegisterWebUser(RegisterWebUserRequest request, CancellationToken cancellationToken)
    {
        try
        {
            var user = await _userService.RegisterWebUserAsync(request, cancellationToken);
            return Ok(user);
        }
        catch (ArgumentException exception)
        {
            return BadRequest(new { message = exception.Message });
        }
        catch (InvalidOperationException exception)
        {
            return Conflict(new { message = exception.Message });
        }
    }

    [HttpGet("pending")]
    [Authorize(Roles = "Backoffice")]
    public Task<IReadOnlyList<UserListItem>> GetPendingUsers(CancellationToken cancellationToken) =>
        _userService.GetPendingUsersAsync(cancellationToken);

    [HttpGet("deactivation-requests")]
    [Authorize(Roles = "Backoffice")]
    public Task<IReadOnlyList<UserListItem>> GetDeactivationRequests(CancellationToken cancellationToken) =>
        _userService.GetDeactivationRequestsAsync(cancellationToken);

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
        if (!CanEditUser(nic))
        {
            return Forbid();
        }

        try
        {
            var user = await _userService.UpdateUserAsync(nic, request, cancellationToken);
            return user is null ? NotFound() : Ok(user);
        }
        catch (InvalidOperationException exception)
        {
            return Conflict(new { message = exception.Message });
        }
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
        if (!User.IsInRole("Backoffice") && !IsCurrentProsumer(nic))
        {
            return Forbid();
        }

        return await SetStatus(nic, AccountStatus.Inactive, cancellationToken);
    }

    [HttpPut("{nic}/request-deactivation")]
    public async Task<IActionResult> RequestDeactivation(string nic, CancellationToken cancellationToken)
    {
        if (!IsCurrentProsumer(nic))
        {
            return Forbid();
        }

        return await SetStatus(nic, AccountStatus.DeactivationRequested, cancellationToken);
    }

    [HttpPut("{nic}/approve-deactivation")]
    [Authorize(Roles = "Backoffice")]
    public async Task<IActionResult> ApproveDeactivation(string nic, CancellationToken cancellationToken)
    {
        return await SetStatus(nic, AccountStatus.Inactive, cancellationToken);
    }

    [HttpPut("{nic}/reject-deactivation")]
    [Authorize(Roles = "Backoffice")]
    public async Task<IActionResult> RejectDeactivation(string nic, CancellationToken cancellationToken)
    {
        return await SetStatus(nic, AccountStatus.Active, cancellationToken);
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

    private bool CanEditUser(string nic) =>
        User.IsInRole("Backoffice") || IsCurrentProsumer(nic);

    private bool IsCurrentProsumer(string nic) =>
        User.IsInRole("Prosumer")
        && string.Equals(User.FindFirstValue(ClaimTypes.NameIdentifier), nic, StringComparison.OrdinalIgnoreCase);
}