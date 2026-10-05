/*
 * File Name: IUserRepository.Login.cs
 * Author: Vidanapathirana L S
 * Student ID: IT23442498
 * Description: Partial interface for user repository login operations.
 */
// User repository login methods
using SolNex.Api.Models;

namespace SolNex.Api.Repositories;

public partial interface IUserRepository
{
    // Find user by NIC or email
    Task<User?> FindByNicOrEmailAsync(string identifier, CancellationToken cancellationToken = default);
}