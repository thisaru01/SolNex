/*
 * File Name: IUserService.cs
 * Author: Vidanapathirana L S
 * Student ID: IT23442498
 * Description: Interface for user and prosumer management service operations.
 */
using SolNex.Api.DTOs;
using SolNex.Api.Models;

namespace SolNex.Api.Services;

/// <summary>
/// Interface for user and prosumer management service operations.
/// </summary>
public interface IUserService
{
    Task<RegisteredUserResponse> RegisterProsumerAsync(
        RegisterUserRequest request,
        CancellationToken cancellationToken = default);
    Task<RegisteredUserResponse> RegisterWebUserAsync(
        RegisterWebUserRequest request,
        CancellationToken cancellationToken = default);
    Task<IReadOnlyList<UserListItem>> GetUsersAsync(string? search, CancellationToken cancellationToken = default);
    Task<IReadOnlyList<UserListItem>> GetPendingUsersAsync(CancellationToken cancellationToken = default);
    Task<IReadOnlyList<UserListItem>> GetDeactivationRequestsAsync(CancellationToken cancellationToken = default);
    Task<UserListItem?> GetUserAsync(string nic, CancellationToken cancellationToken = default);
    Task<UserListItem?> UpdateUserAsync(string nic, UpdateUserRequest request, CancellationToken cancellationToken = default);
    Task<UserListItem?> UpdateRoleAsync(string nic, UpdateUserRoleRequest request, CancellationToken cancellationToken = default);
    Task<UserListItem?> SetStatusAsync(string nic, AccountStatus status, CancellationToken cancellationToken = default);
    
    // Retrieves the list of favorite station IDs for a given user.
    Task<List<string>?> GetFavoritesAsync(string nic, CancellationToken cancellationToken = default);
    
    // Updates the list of favorite station IDs for a given user.
    Task<bool> UpdateFavoritesAsync(string nic, List<string> favoriteIds, CancellationToken cancellationToken = default);
}
