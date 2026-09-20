using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using MongoDB.Driver;
using SolNex.Api.Data;
using SolNex.Api.DTOs;
using SolNex.Api.Models;

namespace SolNex.Api.Controllers;

[ApiController]
[Route("api/auth")]
public class AuthController : ControllerBase
{
    private readonly MongoDbContext _database;
    private readonly IConfiguration _configuration;
    private readonly PasswordHasher<User> _passwordHasher = new();

    public AuthController(MongoDbContext database, IConfiguration configuration)
    {
        _database = database;
        _configuration = configuration;
    }

    [HttpPost("login")]
    public async Task<IActionResult> Login(LoginRequest request)
    {
        var identifier = request.Identifier.Trim();
        var user = await _database.Users
            .Find(user => user.Nic == identifier || user.Email == identifier)
            .FirstOrDefaultAsync();

        if (user is null || user.AccountStatus != AccountStatus.Active)
        {
            return Unauthorized(new { message = "Invalid credentials or inactive account." });
        }

        var passwordResult = _passwordHasher.VerifyHashedPassword(user, user.PasswordHash, request.Password);
        if (passwordResult == PasswordVerificationResult.Failed)
        {
            return Unauthorized(new { message = "Invalid credentials or inactive account." });
        }

        var jwtSettings = _configuration.GetSection("Jwt");
        var key = jwtSettings["Key"];
        if (string.IsNullOrWhiteSpace(key))
        {
            return Problem("JWT signing key is not configured.", statusCode: StatusCodes.Status500InternalServerError);
        }

        var expiresAt = DateTime.UtcNow.AddMinutes(jwtSettings.GetValue("ExpiryMinutes", 60));
        var claims = new[]
        {
            new Claim(JwtRegisteredClaimNames.Sub, user.Nic),
            new Claim(ClaimTypes.NameIdentifier, user.Nic),
            new Claim(ClaimTypes.Name, user.FullName),
            new Claim(ClaimTypes.Role, user.Role.ToString()),
            new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString())
        };

        var credentials = new SigningCredentials(
            new SymmetricSecurityKey(Encoding.UTF8.GetBytes(key)),
            SecurityAlgorithms.HmacSha256);
        var token = new JwtSecurityToken(
            issuer: jwtSettings["Issuer"],
            audience: jwtSettings["Audience"],
            claims: claims,
            expires: expiresAt,
            signingCredentials: credentials);

        return Ok(new
        {
            token = new JwtSecurityTokenHandler().WriteToken(token),
            expiresAt,
            user = new
            {
                nic = user.Nic,
                fullName = user.FullName,
                email = user.Email,
                role = user.Role.ToString()
            }
        });
    }
}