using SolNex.Api.Models;

namespace SolNex.Api.Repositories;

public partial interface IUserRepository
{
    Task<bool> ExistsByNicOrEmailAsync(string nic, string email, CancellationToken cancellationToken = default);
    Task CreateAsync(User user, CancellationToken cancellationToken = default);
    Task<IReadOnlyList<User>> GetAsync(string? search, CancellationToken cancellationToken = default);
    Task<User?> GetByNicAsync(string nic, CancellationToken cancellationToken = default);
    Task<bool> UpdateAsync(User user, CancellationToken cancellationToken = default);
    Task<User?> FindByIdentifierAsync(string identifier, CancellationToken cancellationToken = default);
}
