/*
 * File Name: IUserRepository.cs
 * Author: Vidanapathirana L S
 * Student ID: IT23442498
 * Description: Interface for user repository data access operations.
 */
using SolNex.Api.Models;

namespace SolNex.Api.Repositories;

/// <summary>
/// Interface for user repository data access operations.
/// </summary>
public partial interface IUserRepository
{
    Task<bool> ExistsByNicOrEmailAsync(string nic, string email, CancellationToken cancellationToken = default);
    Task<bool> ExistsByEmailExceptNicAsync(string email, string nic, CancellationToken cancellationToken = default);
    Task CreateAsync(User user, CancellationToken cancellationToken = default);
    Task<IReadOnlyList<User>> GetAsync(string? search, CancellationToken cancellationToken = default);
    Task<User?> GetByNicAsync(string nic, CancellationToken cancellationToken = default);
    Task<bool> UpdateAsync(User user, CancellationToken cancellationToken = default);
    Task<User?> FindByIdentifierAsync(string identifier, CancellationToken cancellationToken = default);
}
