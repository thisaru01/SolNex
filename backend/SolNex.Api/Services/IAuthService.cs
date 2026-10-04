/*
 * File Name: IAuthService.cs
 * Author: Vidanapathirana L S
 * Student ID: IT23442498
 * Description: Interface for user authentication service operations.
 */
using SolNex.Api.DTOs;

namespace SolNex.Api.Services;

/// <summary>
/// Interface for authentication service operations.
/// </summary>
public interface IAuthService
{
    // Authenticate user and return JWT token
    Task<LoginResponse?> LoginAsync(LoginRequest request, CancellationToken cancellationToken = default);
    Task<bool> ResetPasswordAsync(ResetPasswordRequest request, CancellationToken cancellationToken = default);
    // Register new prosumer account
    Task<RegisteredUserResponse> RegisterProsumerAsync(RegisterUserRequest request, CancellationToken cancellationToken = default);
}