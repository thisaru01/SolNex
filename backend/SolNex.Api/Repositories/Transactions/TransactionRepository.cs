using MongoDB.Bson;
using MongoDB.Driver;

using SolNex.Api.Data;
using SolNex.Api.Models;

namespace SolNex.Api.Repositories.Transactions;

public class TransactionRepository :
    ITransactionRepository
{
    private readonly
        IMongoCollection<EnergyTransaction>
        _transactions;

    // Initializes the transaction collection
    // and required unique indexes.
    public TransactionRepository(
        MongoDbContext dbContext)
    {
        _transactions =
            dbContext.EnergyTransactions;

        var indexes =
            new[]
            {
                new CreateIndexModel<
                    EnergyTransaction>(
                    Builders<
                        EnergyTransaction>
                        .IndexKeys
                        .Ascending(
                            transaction =>
                                transaction
                                    .TransactionId),

                    new CreateIndexOptions
                    {
                        Unique = true
                    }),

                new CreateIndexModel<
                    EnergyTransaction>(
                    Builders<
                        EnergyTransaction>
                        .IndexKeys
                        .Ascending(
                            transaction =>
                                transaction
                                    .ReservationId),

                    new CreateIndexOptions
                    {
                        Unique = true
                    }),

                new CreateIndexModel<
                    EnergyTransaction>(
                    Builders<
                        EnergyTransaction>
                        .IndexKeys
                        .Ascending(
                            transaction =>
                                transaction
                                    .QrToken),

                    new CreateIndexOptions
                    {
                        Unique = true
                    })
            };

        _transactions
            .Indexes
            .CreateMany(
                indexes);
    }

    // Gets a transaction using either
    // MongoDB ID or public Transaction ID.
    public async Task<
        EnergyTransaction?>
        GetByIdOrTransactionIdAsync(
            string id)
    {
        if (
            ObjectId.TryParse(
                id,
                out _))
        {
            var byObjectId =
                await _transactions
                    .Find(
                        transaction =>
                            transaction.Id ==
                            id)
                    .FirstOrDefaultAsync();

            if (
                byObjectId !=
                null)
            {
                return byObjectId;
            }
        }

        return await _transactions
            .Find(
                transaction =>
                    transaction
                        .TransactionId ==
                    id)
            .FirstOrDefaultAsync();
    }

    // Finds the transaction whose secure
    // token was scanned from the QR code.
    public async Task<
        EnergyTransaction?>
        GetByQrTokenAsync(
            string qrToken)
    {
        return await _transactions
            .Find(
                transaction =>
                    transaction.QrToken ==
                    qrToken)
            .FirstOrDefaultAsync();
    }

    // Finds the transaction generated
    // for one approved reservation.
    public async Task<
        EnergyTransaction?>
        GetByReservationIdAsync(
            string reservationId)
    {
        return await _transactions
            .Find(
                transaction =>
                    transaction
                        .ReservationId ==
                    reservationId)
            .FirstOrDefaultAsync();
    }

    // Returns transactions that are
    // awaiting verification/completion.
    public async Task<
        IEnumerable<EnergyTransaction>>
        GetPendingAsync()
    {
        var filter =
            Builders<
                EnergyTransaction>
                .Filter
                .In(
                    transaction =>
                        transaction.Status,

                    new[]
                    {
                        TransactionStatus.Pending,
                        TransactionStatus.Verified
                    });

        return await _transactions
            .Find(filter)
            .SortByDescending(
                transaction =>
                    transaction.VerifiedAt)
            .ToListAsync();
    }

    // Returns completed transfers.
    public async Task<
        IEnumerable<EnergyTransaction>>
        GetCompletedAsync()
    {
        return await _transactions
            .Find(
                transaction =>
                    transaction.Status ==
                    TransactionStatus.Completed)
            .SortByDescending(
                transaction =>
                    transaction.CompletedAt)
            .ToListAsync();
    }

    // Returns active approved transactions
    // belonging to one Prosumer.
    public async Task<
        IEnumerable<EnergyTransaction>>
        GetActiveByProsumerNicAsync(
            string nic)
    {
        var statusFilter =
            Builders<
                EnergyTransaction>
                .Filter
                .In(
                    transaction =>
                        transaction.Status,

                    new[]
                    {
                        TransactionStatus.Pending,
                        TransactionStatus.Verified
                    });

        var filter =
            Builders<
                EnergyTransaction>
                .Filter
                .And(

                    Builders<
                        EnergyTransaction>
                        .Filter
                        .Eq(
                            transaction =>
                                transaction.Nic,

                            nic),

                    statusFilter
                );

        return await _transactions
            .Find(filter)
            .SortByDescending(
                transaction =>
                    transaction
                        .TransactionId)
            .ToListAsync();
    }

    // Creates a new transaction.
    public async Task CreateAsync(
        EnergyTransaction transaction)
    {
        await _transactions
            .InsertOneAsync(
                transaction);
    }

    // Saves transaction changes.
    public async Task UpdateAsync(
        EnergyTransaction transaction)
    {
        if (
            string.IsNullOrWhiteSpace(
                transaction.Id))
        {
            throw new InvalidOperationException(
                "Transaction database ID is missing.");
        }

        await _transactions
            .ReplaceOneAsync(
                current =>
                    current.Id ==
                    transaction.Id,

                transaction);
    }
}