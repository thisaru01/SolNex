/*
 * File Name: AuthService.cs
 * Author: Vidanapathirana L S
 * Student ID: IT23442498
 * Description: Service implementation for user authentication business logic.
 */
using Microsoft.AspNetCore.Identity;
using SolNex.Api.DTOs;
using SolNex.Api.Models;
using SolNex.Api.Repositories;

namespace SolNex.Api.Services;

/// <summary>
/// Service for user authentication operations.
/// Handles login, password verification, and JWT token generation.
/// </summary>
public sealed class AuthService : IAuthService
{
    private readonly IUserRepository _userRepository;
    private readonly IPasswordHasher<User> _passwordHasher;
    private readonly IJwtTokenService _jwtTokenService;
    private readonly IUserService _userService;

    public AuthService(
        IUserRepository userRepository,
        IPasswordHasher<User> passwordHasher,
        IJwtTokenService jwtTokenService,
        IUserService userService)
    {
        _userRepository = userRepository;
        _passwordHasher = passwordHasher;
        _jwtTokenService = jwtTokenService;
        _userService = userService;
    }

    // Authenticate user with NIC/email and password, return JWT token if successful
    public async Task<LoginResponse?> LoginAsync(
        LoginRequest request,
        CancellationToken cancellationToken = default)
    {
        var user = await _userRepository.FindByIdentifierAsync(request.Identifier.Trim(), cancellationToken);
        if (user is null || user.AccountStatus != AccountStatus.Active)
        {
            return null;
        }

        var passwordResult = _passwordHasher.VerifyHashedPassword(user, user.PasswordHash, request.Password);
        if (passwordResult == PasswordVerificationResult.Failed)
        {
            return null;
        }

        var token = _jwtTokenService.CreateToken(user);
        return new LoginResponse(
            token.Token,
            token.ExpiresAt,
            user.Nic,
            user.FullName,
            user.Email,
            user.Role.ToString());
    }

    public async Task<bool> ResetPasswordAsync(
        ResetPasswordRequest request,
        CancellationToken cancellationToken = default)
    {
        var user = await _userRepository.FindByIdentifierAsync(request.Identifier.Trim(), cancellationToken);
        if (user is null || !string.Equals(user.Email, request.Email.Trim(), StringComparison.OrdinalIgnoreCase))
        {
            return false;
        }

        user.PasswordHash = _passwordHasher.HashPassword(user, request.NewPassword);
        user.UpdatedAt = DateTime.UtcNow;
        return await _userRepository.UpdateAsync(user, cancellationToken);
    }

    // Register new prosumer account (delegates to UserService)
    public async Task<RegisteredUserResponse> RegisterProsumerAsync(RegisterUserRequest request, CancellationToken cancellationToken = default)
    {
        return await _userService.RegisterProsumerAsync(request, cancellationToken);
    }
}