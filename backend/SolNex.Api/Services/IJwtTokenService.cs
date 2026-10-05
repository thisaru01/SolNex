/*
 * File Name: IJwtTokenService.cs
 * Author: Vidanapathirana L S
 * Student ID: IT23442498
 * Description: Interface for JWT token generation service operations.
 */
using SolNex.Api.Models;

namespace SolNex.Api.Services;

public interface IJwtTokenService
{
    (string Token, DateTime ExpiresAt) CreateToken(User user);
}