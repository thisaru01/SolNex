/*
 * File Name: AuthController.cs
 * Author: Vidanapathirana L S
 * Student ID: IT23442498
 * Description: Controller for user authentication and registration endpoints.
 */
using Microsoft.AspNetCore.Mvc;
using SolNex.Api.DTOs;
using SolNex.Api.Services;

namespace SolNex.Api.Controllers;

/// <summary>
/// Authentication controller for user login and registration.
/// Handles prosumer authentication and account creation.
/// </summary>
[ApiController]
[Route("api/auth")]
public sealed class AuthController : ControllerBase
{
    private readonly IAuthService _authService;

    public AuthController(IAuthService authService)
    {
        _authService = authService;
    }

    // Authenticate user and return JWT token
    [HttpPost("login")]
    [ProducesResponseType(typeof(LoginResponse), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status401Unauthorized)]
    public async Task<IActionResult> Login(LoginRequest request, CancellationToken cancellationToken)
    {
        var response = await _authService.LoginAsync(request, cancellationToken);
        return response is null
            ? Unauthorized(new { message = "Invalid credentials or inactive account." })
            : Ok(response);
    }

    // Register new prosumer account
    [HttpPost("register")]
    [ProducesResponseType(typeof(RegisteredUserResponse), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> Register(RegisterUserRequest request, CancellationToken cancellationToken)
    {
        var response = await _authService.RegisterProsumerAsync(request, cancellationToken);
        return Ok(response);
    }

    [HttpPost("reset-password")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> ResetPassword(ResetPasswordRequest request, CancellationToken cancellationToken)
    {
        var reset = await _authService.ResetPasswordAsync(request, cancellationToken);
        return reset
            ? Ok(new { message = "Password reset successfully. You can now sign in." })
            : BadRequest(new { message = "The identifier and registered email do not match." });
    }
}