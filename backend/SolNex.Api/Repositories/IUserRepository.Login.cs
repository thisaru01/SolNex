using SolNex.Api.Models;

namespace SolNex.Api.Repositories;

public partial interface IUserRepository
{
    Task<User?> FindByNicOrEmailAsync(string identifier, CancellationToken cancellationToken = default);
}