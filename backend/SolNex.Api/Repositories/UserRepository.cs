using MongoDB.Driver;
using SolNex.Api.Data;
using SolNex.Api.Models;

namespace SolNex.Api.Repositories;

public sealed class UserRepository : IUserRepository
{
    private readonly IMongoCollection<User> _users;

    public UserRepository(MongoDbContext dbContext)
    {
        _users = dbContext.Users;
    }

    public Task<bool> ExistsByNicOrEmailAsync(
        string nic,
        string email,
        CancellationToken cancellationToken = default)
    {
        var filter = Builders<User>.Filter.Or(
            Builders<User>.Filter.Eq(user => user.Nic, nic),
            Builders<User>.Filter.Eq(user => user.Email, email));

        return _users.Find(filter).AnyAsync(cancellationToken);
    }

    public Task CreateAsync(User user, CancellationToken cancellationToken = default)
    {
        return _users.InsertOneAsync(user, cancellationToken: cancellationToken);
    }
}