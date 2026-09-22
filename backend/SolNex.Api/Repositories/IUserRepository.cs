using SolNex.Api.Models;

namespace SolNex.Api.Repositories;

public interface IUserRepository
{
    Task<bool> ExistsByNicOrEmailAsync(string nic, string email, CancellationToken cancellationToken = default);
    Task CreateAsync(User user, CancellationToken cancellationToken = default);
}
