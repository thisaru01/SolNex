using Microsoft.AspNetCore.Identity;
using SolNex.Api.DTOs;
using SolNex.Api.Models;
using SolNex.Api.Repositories;

namespace SolNex.Api.Services;

public sealed class AuthService : IAuthService
{
    private readonly IUserRepository _userRepository;
    private readonly IPasswordHasher<User> _passwordHasher;
    private readonly IJwtTokenService _jwtTokenService;

    public AuthService(
        IUserRepository userRepository,
        IPasswordHasher<User> passwordHasher,
        IJwtTokenService jwtTokenService)
    {
        _userRepository = userRepository;
        _passwordHasher = passwordHasher;
        _jwtTokenService = jwtTokenService;
    }

    public async Task<LoginResponse?> LoginAsync(
        LoginRequest request,
        CancellationToken cancellationToken = default)
    {
        var identifier = request.Identifier.Trim();
        var user = await _userRepository.FindByNicOrEmailAsync(identifier, cancellationToken);
        if (user is null || user.AccountStatus != AccountStatus.Active)
        {
            return null;
        }

        var passwordResult = _passwordHasher.VerifyHashedPassword(user, user.PasswordHash, request.Password);
        if (passwordResult == PasswordVerificationResult.Failed)
        {
            return null;
        }

        var token = _jwtTokenService.CreateToken(user);
        return new LoginResponse(
            token.Token,
            token.ExpiresAt,
            user.Nic,
            user.FullName,
            user.Email,
            user.Role.ToString());
    }
}