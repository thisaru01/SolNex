using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SolNex.Api.DTOs;
using SolNex.Api.Models;
using SolNex.Api.Services;

namespace SolNex.Api.Controllers;

[ApiController]
[Route("api/users")]
[Authorize(Roles = "Backoffice")]
public sealed class UserManagementController : ControllerBase
{
    private readonly IUserService _userService;

    public UserManagementController(IUserService userService)
    {
        _userService = userService;
    }

    [HttpGet]
    [AllowAnonymous]
    public Task<IReadOnlyList<UserListItem>> GetUsers([FromQuery] string? search, CancellationToken cancellationToken) =>
        _userService.GetUsersAsync(search, cancellationToken);

    [HttpGet("pending")]
    [AllowAnonymous]
    public Task<IReadOnlyList<UserListItem>> GetPendingUsers(CancellationToken cancellationToken) =>
        _userService.GetPendingUsersAsync(cancellationToken);

    [HttpGet("{nic}")]
    public async Task<IActionResult> GetUser(string nic, CancellationToken cancellationToken)
    {
        var user = await _userService.GetUserAsync(nic, cancellationToken);
        return user is null ? NotFound() : Ok(user);
    }

    [HttpPut("{nic}")]
    public async Task<IActionResult> UpdateUser(string nic, UpdateUserRequest request, CancellationToken cancellationToken)
    {
        var user = await _userService.UpdateUserAsync(nic, request, cancellationToken);
        return user is null ? NotFound() : Ok(user);
    }

    [HttpPut("{nic}/role")]
    public async Task<IActionResult> UpdateRole(string nic, UpdateUserRoleRequest request, CancellationToken cancellationToken)
    {
        var user = await _userService.UpdateRoleAsync(nic, request, cancellationToken);
        return user is null ? BadRequest(new { message = "User or role is invalid." }) : Ok(user);
    }

    [HttpPut("{nic}/activate")]
    public Task<IActionResult> Activate(string nic, CancellationToken cancellationToken) => SetStatus(nic, AccountStatus.Active, cancellationToken);

    [HttpPut("{nic}/deactivate")]
    public Task<IActionResult> Deactivate(string nic, CancellationToken cancellationToken) => SetStatus(nic, AccountStatus.Inactive, cancellationToken);

    private async Task<IActionResult> SetStatus(string nic, AccountStatus status, CancellationToken cancellationToken)
    {
        var user = await _userService.SetStatusAsync(nic, status, cancellationToken);
        return user is null ? NotFound() : Ok(user);
    }
}