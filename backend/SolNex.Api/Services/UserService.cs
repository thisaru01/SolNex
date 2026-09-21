using Microsoft.AspNetCore.Identity;
using MongoDB.Driver;
using SolNex.Api.DTOs;
using SolNex.Api.Models;
using SolNex.Api.Repositories;

namespace SolNex.Api.Services;

public sealed class UserService : IUserService
{
    private readonly IUserRepository _userRepository;
    private readonly IPasswordHasher<User> _passwordHasher;

    public UserService(
        IUserRepository userRepository,
        IPasswordHasher<User> passwordHasher)
    {
        _userRepository = userRepository;
        _passwordHasher = passwordHasher;
    }

    public async Task<RegisteredUserResponse> RegisterProsumerAsync(
        RegisterUserRequest request,
        CancellationToken cancellationToken = default)
    {
        var nic = request.Nic.Trim();
        var email = request.Email.Trim().ToLowerInvariant();

        if (await _userRepository.ExistsByNicOrEmailAsync(nic, email, cancellationToken))
        {
            throw new InvalidOperationException("A user with this NIC or email already exists.");
        }

        var now = DateTime.UtcNow;
        var user = new User
        {
            Nic = nic,
            FullName = request.FullName.Trim(),
            Email = email,
            Phone = request.Phone.Trim(),
            Role = UserRole.Prosumer,
            AccountStatus = AccountStatus.Pending,
            CreatedAt = now,
            UpdatedAt = now
        };
        user.PasswordHash = _passwordHasher.HashPassword(user, request.Password);

        try
        {
            await _userRepository.CreateAsync(user, cancellationToken);
        }
        catch (MongoWriteException exception) when (exception.WriteError?.Category == ServerErrorCategory.DuplicateKey)
        {
            throw new InvalidOperationException("A user with this NIC or email already exists.", exception);
        }

        return new RegisteredUserResponse(
            user.Nic,
            user.FullName,
            user.Email,
            user.Phone,
            user.Role.ToString(),
            user.AccountStatus.ToString(),
            user.CreatedAt);
    }
}