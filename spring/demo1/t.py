import datetime

def format_nano_timestamp(nano_timestamp):
    dt = datetime.datetime.fromtimestamp(nano_timestamp / 1e9)
    return '{:.2e}'.format(dt.timestamp())

# Example usage
nano_timestamp = time.time_ns()
# nano_timestamp = 1558566400123456789
formatted_timestamp = format_nano_timestamp(nano_timestamp)
print(formatted_timestamp)  # Output: 2.00e+11
