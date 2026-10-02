using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SolNex.Api.DTOs;
using SolNex.Api.Models;
using SolNex.Api.Services;

namespace SolNex.Api.Controllers;

/// <summary>
/// User management controller for handling user and prosumer operations.
/// Provides endpoints for user registration, retrieval, updates, role management, and account status control.
/// </summary>
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

    // Get all users with optional search filter (Backoffice only)
    [HttpGet]
    [Authorize(Roles = "Backoffice")]
    public Task<IReadOnlyList<UserListItem>> GetUsers([FromQuery] string? search, CancellationToken cancellationToken) =>
        _userService.GetUsersAsync(search, cancellationToken);

    // Register a new web user (Backoffice only)
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

    // Get pending users awaiting activation (Backoffice only)
    [HttpGet("pending")]
    [Authorize(Roles = "Backoffice")]
    public Task<IReadOnlyList<UserListItem>> GetPendingUsers(CancellationToken cancellationToken) =>
        _userService.GetPendingUsersAsync(cancellationToken);

    // Get users with deactivation requests (Backoffice only)
    [HttpGet("deactivation-requests")]
    [Authorize(Roles = "Backoffice")]
    public Task<IReadOnlyList<UserListItem>> GetDeactivationRequests(CancellationToken cancellationToken) =>
        _userService.GetDeactivationRequestsAsync(cancellationToken);

    // Get user by NIC (accessible by Backoffice or own user)
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

    // Update user information (Backoffice or own prosumer)
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

    // Update user role (Backoffice only)
    [HttpPut("{nic}/role")]
    [Authorize(Roles = "Backoffice")]
    public async Task<IActionResult> UpdateRole(string nic, UpdateUserRoleRequest request, CancellationToken cancellationToken)
    {
        var user = await _userService.UpdateRoleAsync(nic, request, cancellationToken);
        return user is null ? BadRequest(new { message = "User or role is invalid." }) : Ok(user);
    }

    // Activate user account (Backoffice only)
    [HttpPut("{nic}/activate")]
    [Authorize(Roles = "Backoffice")]
    public Task<IActionResult> Activate(string nic, CancellationToken cancellationToken) => SetStatus(nic, AccountStatus.Active, cancellationToken);

    // Deactivate user account (Backoffice or own prosumer)
    [HttpPut("{nic}/deactivate")]
    public async Task<IActionResult> Deactivate(string nic, CancellationToken cancellationToken)
    {
        if (!User.IsInRole("Backoffice") && !IsCurrentProsumer(nic))
        {
            return Forbid();
        }

        return await SetStatus(nic, AccountStatus.Inactive, cancellationToken);
    }

    // Request account deactivation (prosumer only for own account)
    [HttpPut("{nic}/request-deactivation")]
    public async Task<IActionResult> RequestDeactivation(string nic, CancellationToken cancellationToken)
    {
        if (!IsCurrentProsumer(nic))
        {
            return Forbid();
        }

        return await SetStatus(nic, AccountStatus.DeactivationRequested, cancellationToken);
    }

    // Approve deactivation request (Backoffice only)
    [HttpPut("{nic}/approve-deactivation")]
    [Authorize(Roles = "Backoffice")]
    public async Task<IActionResult> ApproveDeactivation(string nic, CancellationToken cancellationToken)
    {
        return await SetStatus(nic, AccountStatus.Inactive, cancellationToken);
    }

    // Reject deactivation request and reactivate account (Backoffice only)
    [HttpPut("{nic}/reject-deactivation")]
    [Authorize(Roles = "Backoffice")]
    public async Task<IActionResult> RejectDeactivation(string nic, CancellationToken cancellationToken)
    {
        return await SetStatus(nic, AccountStatus.Active, cancellationToken);
    }

    // Helper method to set account status
    private async Task<IActionResult> SetStatus(string nic, AccountStatus status, CancellationToken cancellationToken)
    {
        var user = await _userService.SetStatusAsync(nic, status, cancellationToken);
        return user is null ? NotFound() : Ok(user);
    }

    // Check if current user can access the target user
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

    // Check if current user can edit the target user
    private bool CanEditUser(string nic) =>
        User.IsInRole("Backoffice") || IsCurrentProsumer(nic);

    // Check if current user is the prosumer matching the NIC
    private bool IsCurrentProsumer(string nic) =>
        User.IsInRole("Prosumer")
        && string.Equals(User.FindFirstValue(ClaimTypes.NameIdentifier), nic, StringComparison.OrdinalIgnoreCase);

    // GET: api/users/me/favorites
    // Retrieves the currently authenticated user's favorite station IDs.
    [HttpGet("me/favorites")]
    [Authorize]
    public async Task<ActionResult<IEnumerable<string>>> GetFavorites(CancellationToken cancellationToken)
    {
        var nic = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (string.IsNullOrEmpty(nic)) return Unauthorized();

        var favorites = await _userService.GetFavoritesAsync(nic, cancellationToken);
        if (favorites == null) return NotFound();

        return Ok(favorites);
    }

    // PUT: api/users/me/favorites
    // Replaces the currently authenticated user's favorite station IDs with the provided list.
    [HttpPut("me/favorites")]
    [Authorize]
    public async Task<IActionResult> UpdateFavorites([FromBody] List<string> favoriteIds, CancellationToken cancellationToken)
    {
        var nic = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (string.IsNullOrEmpty(nic)) return Unauthorized();

        var success = await _userService.UpdateFavoritesAsync(nic, favoriteIds, cancellationToken);
        if (!success) return NotFound();

        return NoContent();
    }
}