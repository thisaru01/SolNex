/*
 * File Name: JwtAuthenticationExtensions.cs
 * Author: Vidanapathirana L S
 * Student ID: IT23442498
 * Description: Extension methods for configuring JWT authentication.
 */
using System.Text;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;

namespace SolNex.Api.Extensions;

/// <summary>
/// JWT authentication configuration extensions.
/// Sets up JWT bearer authentication for the API.
/// </summary>
public static class JwtAuthenticationExtensions
{
    // Configure JWT bearer authentication with signing key and validation parameters
    public static IServiceCollection AddSolNexJwtAuthentication(
        this IServiceCollection services,
        IConfiguration configuration)
    {
        var settings = configuration.GetSection("Jwt");
        var key = settings["Key"];
        if (string.IsNullOrWhiteSpace(key))
        {
            throw new InvalidOperationException("JWT signing key is missing. Configure Jwt:Key with user-secrets.");
        }

        services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
            .AddJwtBearer(options => options.TokenValidationParameters = new TokenValidationParameters
            {
                ValidateIssuerSigningKey = true,
                IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(key)),
                ValidateIssuer = true,
                ValidIssuer = settings["Issuer"],
                ValidateAudience = true,
                ValidAudience = settings["Audience"],
                ValidateLifetime = true,
                ClockSkew = TimeSpan.Zero
            });

        services.AddAuthorization();
        return services;
    }
}