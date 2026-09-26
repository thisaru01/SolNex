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

    public async Task<RegisteredUserResponse> RegisterWebUserAsync(
        RegisterWebUserRequest request,
        CancellationToken cancellationToken = default)
    {
        var nic = request.Nic.Trim();
        var email = request.Email.Trim().ToLowerInvariant();

        if (!Enum.TryParse<UserRole>(request.Role, true, out var role))
        {
            throw new ArgumentException("Invalid role.");
        }

        if (await _userRepository.ExistsByNicOrEmailAsync(nic, email, cancellationToken))
        {
            throw new InvalidOperationException("A user with this NIC or email already exists.");
        }

        var now = DateTime.UtcNow;
        var accountStatus = role == UserRole.Prosumer ? AccountStatus.Pending : AccountStatus.Active;
        
        var user = new User
        {
            Nic = nic,
            FullName = request.FullName.Trim(),
            Email = email,
            Phone = request.Phone.Trim(),
            Role = role,
            AccountStatus = accountStatus,
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

    public async Task<IReadOnlyList<UserListItem>> GetUsersAsync(string? search, CancellationToken cancellationToken = default)
    {
        var users = await _userRepository.GetAsync(search, cancellationToken);
        return users.Select(Map).ToList();
    }

    public async Task<IReadOnlyList<UserListItem>> GetPendingUsersAsync(CancellationToken cancellationToken = default)
    {
        var users = await _userRepository.GetAsync(null, cancellationToken);
        return users.Where(user => user.AccountStatus == AccountStatus.Pending).Select(Map).ToList();
    }

    public async Task<UserListItem?> GetUserAsync(string nic, CancellationToken cancellationToken = default)
    {
        var user = await _userRepository.GetByNicAsync(nic.Trim(), cancellationToken);
        return user is null ? null : Map(user);
    }

    public async Task<UserListItem?> UpdateUserAsync(string nic, UpdateUserRequest request, CancellationToken cancellationToken = default)
    {
        var user = await _userRepository.GetByNicAsync(nic.Trim(), cancellationToken);
        if (user is null) return null;

        user.FullName = request.FullName.Trim();
        user.Email = request.Email.Trim().ToLowerInvariant();
        user.Phone = request.Phone.Trim();
        user.UpdatedAt = DateTime.UtcNow;
        await _userRepository.UpdateAsync(user, cancellationToken);
        return Map(user);
    }

    public async Task<UserListItem?> UpdateRoleAsync(string nic, UpdateUserRoleRequest request, CancellationToken cancellationToken = default)
    {
        var user = await _userRepository.GetByNicAsync(nic.Trim(), cancellationToken);
        if (user is null || !Enum.TryParse<UserRole>(request.Role, true, out var role)) return null;

        user.Role = role;
        user.UpdatedAt = DateTime.UtcNow;
        await _userRepository.UpdateAsync(user, cancellationToken);
        return Map(user);
    }

    public async Task<UserListItem?> SetStatusAsync(string nic, AccountStatus status, CancellationToken cancellationToken = default)
    {
        var user = await _userRepository.GetByNicAsync(nic.Trim(), cancellationToken);
        if (user is null) return null;

        user.AccountStatus = status;
        user.UpdatedAt = DateTime.UtcNow;
        await _userRepository.UpdateAsync(user, cancellationToken);
        return Map(user);
    }

    private static UserListItem Map(User user) => new(
        user.Nic,
        user.FullName,
        user.Email,
        user.Phone,
        user.Role.ToString(),
        user.AccountStatus.ToString(),
        user.CreatedAt,
        user.UpdatedAt);
}
