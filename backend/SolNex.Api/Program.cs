using MongoDB.Driver;
using Microsoft.AspNetCore.Identity;
using SolNex.Api.Data;
using SolNex.Api.Extensions;
using SolNex.Api.Models;
using SolNex.Api.Repositories;
using SolNex.Api.Repositories.Stations;
using SolNex.Api.Services;
using SolNex.Api.Services.Stations;

var builder = WebApplication.CreateBuilder(args);

// Register the MongoDbContext as a Singleton service
builder.Services.AddSingleton<MongoDbContext>();

// Register Repositories and Services
builder.Services.AddScoped<IStationRepository, StationRepository>();
builder.Services.AddScoped<IStationService, StationService>();
builder.Services.AddScoped<IEnergyBookingSlotRepository, EnergyBookingSlotRepository>();
builder.Services.AddScoped<IEnergyReservationRepository, EnergyReservationRepository>();
builder.Services.AddScoped<IEnergyBookingSlotService, EnergyBookingSlotService>();
builder.Services.AddScoped<IEnergyReservationService, EnergyReservationService>();
builder.Services.AddScoped<IUserRepository, UserRepository>();
builder.Services.AddScoped<IUserService, UserService>();
builder.Services.AddScoped<IPasswordHasher<User>, PasswordHasher<User>>();
builder.Services.AddSolNexJwtAuthentication(builder.Configuration);
builder.Services.AddScoped<IAuthService, AuthService>();
builder.Services.AddScoped<IJwtTokenService, JwtTokenService>();

// Configure CORS
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

builder.Services.AddControllers();
// Learn more about configuring OpenAPI at https://aka.ms/aspnet/openapi
builder.Services.AddOpenApi();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseCors("AllowAll");
app.UseHttpsRedirection();
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

var summaries = new[]
{
    "Freezing", "Bracing", "Chilly", "Cool", "Mild", "Warm", "Balmy", "Hot", "Sweltering", "Scorching"
};

app.MapGet("/weatherforecast", () =>
{
    var forecast =  Enumerable.Range(1, 5).Select(index =>
        new WeatherForecast
        (
            DateOnly.FromDateTime(DateTime.Now.AddDays(index)),
            Random.Shared.Next(-20, 55),
            summaries[Random.Shared.Next(summaries.Length)]
        ))
        .ToArray();
    return forecast;
})
.WithName("GetWeatherForecast");

// Endpoint to test MongoDB connectivity
app.MapGet("/test-db", async (MongoDbContext dbContext) =>
{
    try
    {
        // The ping command is a lightweight way to check the connection
        var result = await dbContext.Database.RunCommandAsync<MongoDB.Bson.BsonDocument>(new MongoDB.Bson.BsonDocument("ping", 1));
        return Results.Ok(new { status = "success", message = "Connected to MongoDB successfully via MongoDbContext!", details = result.ToString() });
    }
    catch (Exception ex)
    {
        return Results.Problem(detail: ex.Message, title: "Database Connection Failed");
    }
});

app.Run();

record WeatherForecast(DateOnly Date, int TemperatureC, string? Summary)
{
    public int TemperatureF => 32 + (int)(TemperatureC / 0.5556);
}
