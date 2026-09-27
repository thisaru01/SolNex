using SolNex.Api.DTOs;
using SolNex.Api.Models;

namespace SolNex.Api.Services;

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
    Task<UserListItem?> GetUserAsync(string nic, CancellationToken cancellationToken = default);
    Task<UserListItem?> UpdateUserAsync(string nic, UpdateUserRequest request, CancellationToken cancellationToken = default);
    Task<UserListItem?> UpdateRoleAsync(string nic, UpdateUserRoleRequest request, CancellationToken cancellationToken = default);
    Task<UserListItem?> SetStatusAsync(string nic, AccountStatus status, CancellationToken cancellationToken = default);
}
